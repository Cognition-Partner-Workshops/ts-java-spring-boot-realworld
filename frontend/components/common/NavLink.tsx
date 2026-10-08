import Link from "next/link";
import { useRouter } from "next/router";
import React from "react";

interface NavLinkProps {
  href: string;
  as?: string;
  children: React.ReactNode;
}

const NavLink = ({ href, as, children }: NavLinkProps) => {
  const router = useRouter();
  const { asPath } = router;
  const target = as ?? href;
  const isActive = encodeURIComponent(asPath) === encodeURIComponent(target);

  return (
    <Link href={target} className={isActive ? "nav-link active" : "nav-link"}>
      {children}
    </Link>
  );
};

export default NavLink;
