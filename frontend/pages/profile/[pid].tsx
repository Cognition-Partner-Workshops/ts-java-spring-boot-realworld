import { useRouter } from "next/router";
import React from "react";
import useSWR, { mutate } from "swr";

import ArticleList from "../../components/article/ArticleList";
import CustomImage from "../../components/common/CustomImage";
import ErrorMessage from "../../components/common/ErrorMessage";
import Maybe from "../../components/common/Maybe";
import EditProfileButton from "../../components/profile/EditProfileButton";
import FollowUserButton from "../../components/profile/FollowUserButton";
import ProfileTab from "../../components/profile/ProfileTab";
import UserAPI from "../../lib/api/user";
import { Author } from "../../lib/types/articleType";
import checkLogin from "../../lib/utils/checkLogin";
import { SERVER_BASE_URL } from "../../lib/utils/constant";
import fetcher from "../../lib/utils/fetcher";
import storage from "../../lib/utils/storage";

interface ProfileResponse {
  profile: Author;
}

interface ProfileProps {
  initialProfile: ProfileResponse;
}

const Profile = ({ initialProfile }: ProfileProps) => {
  const router = useRouter();
  const {
    query: { pid },
  } = router;

  const profileKey = `${SERVER_BASE_URL}/profiles/${encodeURIComponent(
    String(pid)
  )}`;

  const { data: fetchedProfile, error: profileError } = useSWR<ProfileResponse>(
    profileKey,
    fetcher,
    { fallbackData: initialProfile }
  );
  const { data: currentUser } = useSWR("user", storage);

  if (profileError) return <ErrorMessage message="Can't load profile" />;

  const { profile } = fetchedProfile || initialProfile;
  const { username, bio, image, following } = profile;

  const isLoggedIn = checkLogin(currentUser);
  const isUser = !!currentUser && username === currentUser?.username;

  const handleFollow = async () => {
    mutate(
      profileKey,
      { profile: { ...profile, following: true } },
      { revalidate: false }
    );
    await UserAPI.follow(String(pid));
    mutate(profileKey);
  };

  const handleUnfollow = async () => {
    mutate(
      profileKey,
      { profile: { ...profile, following: false } },
      { revalidate: false }
    );
    await UserAPI.unfollow(String(pid));
    mutate(profileKey);
  };

  return (
    <div className="profile-page">
      <div className="user-info">
        <div className="container">
          <div className="row">
            <div className="col-xs-12 col-md-10 offset-md-1">
              <CustomImage
                src={image}
                alt="User's profile image"
                className="user-img"
              />
              <h4>{username}</h4>
              <p>{bio}</p>
              <EditProfileButton isUser={isUser} />
              <Maybe test={isLoggedIn}>
                <FollowUserButton
                  isUser={isUser}
                  username={username}
                  following={following}
                  follow={handleFollow}
                  unfollow={handleUnfollow}
                />
              </Maybe>
            </div>
          </div>
        </div>
      </div>

      <div className="container">
        <div className="row">
          <div className="col-xs-12 col-md-10 offset-md-1">
            <div className="articles-toggle">
              <ProfileTab profile={profile} />
            </div>
            <ArticleList />
          </div>
        </div>
      </div>
    </div>
  );
};

Profile.getInitialProps = async ({
  query: { pid },
}: {
  query: { pid?: string | string[] };
}) => {
  const { data: initialProfile } = await UserAPI.get(String(pid));
  return { initialProfile };
};

export default Profile;
