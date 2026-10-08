import React from "react";

interface FollowUserButtonProps {
  isUser: boolean;
  following: boolean;
  username: string;
  follow: (username: string) => void;
  unfollow: (username: string) => void;
}

const FollowUserButton = ({
  isUser,
  following,
  username,
  follow,
  unfollow,
}: FollowUserButtonProps) => {
  if (isUser) {
    return null;
  }

  const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    if (following) {
      unfollow(username);
    } else {
      follow(username);
    }
  };

  return (
    <button
      className={`btn btn-sm action-btn ${
        following ? "btn-secondary" : "btn-outline-secondary"
      }`}
      onClick={handleClick}
    >
      <i className="ion-plus-round" />
      &nbsp;
      {following ? "Unfollow" : "Follow"} {username}
    </button>
  );
};

export default FollowUserButton;
