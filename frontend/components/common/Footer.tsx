import React from "react";

import CustomLink from "./CustomLink";

const Footer = () => (
  <footer>
    <div className="container">
      <CustomLink href="/" className="logo-font">
        conduit
      </CustomLink>
      <span className="attribution">
        An interactive learning project from{" "}
        <a href="https://thinkster.io">Thinkster</a>. Code &amp; design licensed
        under MIT.
      </span>
    </div>
  </footer>
);

export default Footer;
