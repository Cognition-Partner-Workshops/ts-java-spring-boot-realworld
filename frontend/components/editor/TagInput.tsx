import React from "react";

interface TagInputProps {
  tagList: string[];
  addTag: (tag: string) => void;
  removeTag: (tag: string) => void;
}

const TagInput = ({ tagList, addTag, removeTag }: TagInputProps) => {
  const [tag, setTag] = React.useState("");

  const changeTagInput = (e: React.ChangeEvent<HTMLInputElement>) =>
    setTag(e.target.value);

  const handleAddTag = () => {
    if (tag) {
      addTag(tag);
      setTag("");
    }
  };

  const handleTagInputKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    switch (e.key) {
      case "Enter":
      case "Tab":
      case ",":
        if (e.key !== "Tab") e.preventDefault();
        handleAddTag();
        break;
      default:
        break;
    }
  };

  const handleRemoveTag = (value: string) => {
    removeTag(value);
  };

  return (
    <>
      <fieldset className="form-group">
        <input
          className="form-control"
          type="text"
          placeholder="Enter tags"
          value={tag}
          onChange={changeTagInput}
          onBlur={handleAddTag}
          onKeyDown={handleTagInputKeyDown}
        />

        <div className="tag-list">
          {tagList.map((value) => (
            <span className="tag-default tag-pill" key={value}>
              <i
                className="ion-close-round"
                role="button"
                aria-label={`Remove tag ${value}`}
                onClick={() => handleRemoveTag(value)}
              />
              {value}
            </span>
          ))}
        </div>
      </fieldset>
    </>
  );
};

export default TagInput;
