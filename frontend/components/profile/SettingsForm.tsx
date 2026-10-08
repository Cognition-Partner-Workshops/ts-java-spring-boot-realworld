import axios from "axios";
import Router from "next/router";
import React from "react";
import useSWR, { mutate } from "swr";

import ListErrors, { ErrorMap } from "../common/ListErrors";
import checkLogin from "../../lib/utils/checkLogin";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import storage from "../../lib/utils/storage";

interface UserInfo {
  image: string;
  username: string;
  bio: string;
  email: string;
  password: string;
}

const SettingsForm = () => {
  const [isLoading, setLoading] = React.useState(false);
  const [errors, setErrors] = React.useState<ErrorMap>({});
  const [userInfo, setUserInfo] = React.useState<UserInfo>({
    image: "",
    username: "",
    bio: "",
    email: "",
    password: "",
  });

  const { data: currentUser } = useSWR("user", storage);
  const isLoggedIn = checkLogin(currentUser);

  React.useEffect(() => {
    if (!isLoggedIn) return;
    setUserInfo((previous) => ({
      ...previous,
      image: currentUser?.image ?? "",
      username: currentUser?.username ?? "",
      bio: currentUser?.bio ?? "",
      email: currentUser?.email ?? "",
    }));
  }, [isLoggedIn, currentUser]);

  const updateState =
    (field: keyof UserInfo) =>
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      const { value } = e.target;
      setUserInfo((previous) => ({ ...previous, [field]: value }));
    };

  const submitForm = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);

    const user: Partial<UserInfo> = { ...userInfo };

    if (!user.password) {
      delete user.password;
    }

    try {
      const { data, status } = await axios.put(
        `${SERVER_BASE_URL}/user`,
        JSON.stringify({ user }),
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Token ${currentUser?.token}`,
          },
          validateStatus: () => true,
        }
      );

      if (status !== 200) {
        setErrors(data?.errors?.body ?? data?.errors ?? {});
      }

      if (data?.user) {
        window.localStorage.setItem("user", JSON.stringify(data.user));
        mutate("user", data.user);
        Router.push(`/`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <React.Fragment>
      <ListErrors errors={errors} />
      <form onSubmit={submitForm}>
        <fieldset>
          <fieldset className="form-group">
            <input
              className="form-control"
              type="text"
              placeholder="URL of profile picture"
              value={userInfo.image}
              onChange={updateState("image")}
            />
          </fieldset>

          <fieldset className="form-group">
            <input
              className="form-control form-control-lg"
              type="text"
              placeholder="Username"
              value={userInfo.username}
              onChange={updateState("username")}
            />
          </fieldset>

          <fieldset className="form-group">
            <textarea
              className="form-control form-control-lg"
              rows={8}
              placeholder="Short bio about you"
              value={userInfo.bio}
              onChange={updateState("bio")}
            />
          </fieldset>

          <fieldset className="form-group">
            <input
              className="form-control form-control-lg"
              type="email"
              placeholder="Email"
              value={userInfo.email}
              onChange={updateState("email")}
            />
          </fieldset>

          <fieldset className="form-group">
            <input
              className="form-control form-control-lg"
              type="password"
              placeholder="New Password"
              value={userInfo.password}
              onChange={updateState("password")}
            />
          </fieldset>

          <button
            className="btn btn-lg btn-primary pull-xs-right"
            type="submit"
            disabled={isLoading}
          >
            Update Settings
          </button>
        </fieldset>
      </form>
    </React.Fragment>
  );
};

export default SettingsForm;
