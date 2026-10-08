import React from "react";

import Navbar from "./Navbar";
import Footer from "./Footer";

interface LayoutProps {
  children: React.ReactNode;
}

const Layout = ({ children }: LayoutProps) => (
  <>
    <Navbar />
    {children}
    <Footer />
  </>
);

export default Layout;
