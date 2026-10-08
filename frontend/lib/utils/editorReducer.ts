export interface EditorState {
  title: string;
  description: string;
  body: string;
  tagList: string[];
}

export type EditorAction =
  | { type: "SET_TITLE"; text: string }
  | { type: "SET_DESCRIPTION"; text: string }
  | { type: "SET_BODY"; text: string }
  | { type: "ADD_TAG"; tag: string }
  | { type: "REMOVE_TAG"; tag: string };

const editorReducer = (
  state: EditorState,
  action: EditorAction
): EditorState => {
  switch (action.type) {
    case "SET_TITLE":
      return {
        ...state,
        title: action.text,
      };
    case "SET_DESCRIPTION":
      return {
        ...state,
        description: action.text,
      };
    case "SET_BODY":
      return {
        ...state,
        body: action.text,
      };
    case "ADD_TAG":
      return {
        ...state,
        tagList: state.tagList.concat(action.tag),
      };
    case "REMOVE_TAG":
      return {
        ...state,
        tagList: state.tagList.filter((tag) => tag !== action.tag),
      };
    default:
      throw new Error("Unhandled action");
  }
};

export default editorReducer;
