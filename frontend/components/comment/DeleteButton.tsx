import axios from "axios";
import { useRouter } from "next/router";
import React from "react";
import useSWR, { mutate } from "swr";

import { SERVER_BASE_URL } from "../../lib/utils/constant";
import storage from "../../lib/utils/storage";

interface DeleteButtonProps {
  commentId: string;
}

const DeleteButton = ({ commentId }: DeleteButtonProps) => {
  const { data: currentUser } = useSWR("user", storage);
  const router = useRouter();
  const {
    query: { pid },
  } = router;

  const handleDelete = async (id: string) => {
    await axios.delete(`${SERVER_BASE_URL}/articles/${pid}/comments/${id}`, {
      headers: {
        Authorization: `Token ${currentUser?.token}`,
      },
    });
    mutate(`${SERVER_BASE_URL}/articles/${pid}/comments`);
  };

  return (
    <span className="mod-options">
      <i
        className="ion-trash-a"
        role="button"
        aria-label="Delete comment"
        onClick={() => handleDelete(commentId)}
      />
    </span>
  );
};

export default DeleteButton;
