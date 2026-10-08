import React from "react";

import { DEFAULT_PROFILE_IMAGE } from "./constant";

const handleBrokenImage = (e: React.SyntheticEvent<HTMLImageElement>) => {
  const target = e.currentTarget;
  target.src = DEFAULT_PROFILE_IMAGE;
  target.onerror = null;
};

export default handleBrokenImage;
