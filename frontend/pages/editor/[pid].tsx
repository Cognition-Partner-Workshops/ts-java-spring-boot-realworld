import axios from "axios";
import Router, { useRouter } from "next/router";
import React from "react";
import useSWR from "swr";

import ListErrors, { ErrorMap } from "../../components/common/ListErrors";
import TagInput from "../../components/editor/TagInput";
import ArticleAPI from "../../lib/api/article";
import { ArticleType } from "../../lib/types/articleType";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import editorReducer, { EditorState } from "../../lib/utils/editorReducer";
import toErrorMap from "../../lib/utils/errors";
import invalidateArticles from "../../lib/utils/invalidateArticles";
import storage from "../../lib/utils/storage";

interface UpdateArticleEditorProps {
  article: ArticleType;
}

const UpdateArticleEditor = ({
  article: initialArticle,
}: UpdateArticleEditorProps) => {
  const initialState: EditorState = {
    title: initialArticle.title,
    description: initialArticle.description,
    body: initialArticle.body,
    tagList: initialArticle.tagList,
  };

  const [isLoading, setLoading] = React.useState(false);
  const [errors, setErrors] = React.useState<ErrorMap>({});
  const [posting, dispatch] = React.useReducer(editorReducer, initialState);
  const { data: currentUser } = useSWR("user", storage);
  const router = useRouter();
  const {
    query: { pid },
  } = router;

  const handleTitle = (e: React.ChangeEvent<HTMLInputElement>) =>
    dispatch({ type: "SET_TITLE", text: e.target.value });
  const handleDescription = (e: React.ChangeEvent<HTMLInputElement>) =>
    dispatch({ type: "SET_DESCRIPTION", text: e.target.value });
  const handleBody = (e: React.ChangeEvent<HTMLTextAreaElement>) =>
    dispatch({ type: "SET_BODY", text: e.target.value });
  const addTag = (tag: string) => dispatch({ type: "ADD_TAG", tag });
  const removeTag = (tag: string) => dispatch({ type: "REMOVE_TAG", tag });

  const handleSubmit = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    setLoading(true);

    const { data, status } = await axios.put(
      `${SERVER_BASE_URL}/articles/${pid}`,
      JSON.stringify({ article: posting }),
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Token ${encodeURIComponent(currentUser?.token)}`,
        },
        validateStatus: () => true,
      }
    );
    setLoading(false);

    if (status !== 200) {
      setErrors(toErrorMap(data));
      return;
    }

    await invalidateArticles();
    Router.push(`/`);
  };

  return (
    <div className="editor-page">
      <div className="container page">
        <div className="row">
          <div className="col-md-10 offset-md-1 col-xs-12">
            <ListErrors errors={errors} />

            <form>
              <fieldset>
                <fieldset className="form-group">
                  <input
                    className="form-control form-control-lg"
                    type="text"
                    placeholder="Article Title"
                    value={posting.title}
                    onChange={handleTitle}
                  />
                </fieldset>

                <fieldset className="form-group">
                  <input
                    className="form-control"
                    type="text"
                    placeholder="What's this article about?"
                    value={posting.description}
                    onChange={handleDescription}
                  />
                </fieldset>

                <fieldset className="form-group">
                  <textarea
                    className="form-control"
                    rows={8}
                    placeholder="Write your article (in markdown)"
                    value={posting.body}
                    onChange={handleBody}
                  />
                </fieldset>

                <TagInput
                  tagList={posting.tagList}
                  addTag={addTag}
                  removeTag={removeTag}
                />

                <button
                  className="btn btn-lg pull-xs-right btn-primary"
                  type="button"
                  disabled={isLoading}
                  onClick={handleSubmit}
                >
                  Update Article
                </button>
              </fieldset>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

UpdateArticleEditor.getInitialProps = async ({
  query: { pid },
}: {
  query: { pid?: string | string[] };
}) => {
  const {
    data: { article },
  } = await ArticleAPI.get(pid);
  return { article };
};

export default UpdateArticleEditor;
