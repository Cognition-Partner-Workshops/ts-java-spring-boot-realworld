import React from "react";

import {
  DEFAULT_PROFILE_IMAGE,
  DEFAULT_IMAGE_SOURCE,
} from "../../lib/utils/constant";
import handleBrokenImage from "../../lib/utils/handleBrokenImage";

interface CustomImageProps {
  src: string | undefined;
  alt: string;
  className?: string;
}

const CustomImage = ({ src, alt, className }: CustomImageProps) => (
  // eslint-disable-next-line @next/next/no-img-element -- lazysizes handles lazy loading of remote avatars
  <img
    data-sizes="auto"
    data-src={src || DEFAULT_PROFILE_IMAGE}
    src={DEFAULT_IMAGE_SOURCE}
    alt={alt}
    className={className ? `${className} lazyload` : `lazyload`}
    onError={handleBrokenImage}
  />
);

export default CustomImage;
