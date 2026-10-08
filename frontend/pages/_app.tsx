import type { AppProps } from "next/app";
import Head from "next/head";
import React from "react";

import Layout from "../components/common/Layout";
import ContextProvider from "../lib/context";
import "../conduit.css";
import "../styles.css";

const MyApp = ({ Component, pageProps }: AppProps) => {
  React.useEffect(() => {
    Promise.all([
      import("lazysizes/plugins/attrchange/ls.attrchange.js"),
      import("lazysizes/plugins/respimg/ls.respimg.js"),
      import("lazysizes"),
    ]).catch(() => undefined);
  }, []);

  return (
    <>
      <Head>
        <meta
          name="viewport"
          content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"
        />
      </Head>
      <ContextProvider>
        <Layout>
          <Component {...pageProps} />
        </Layout>
      </ContextProvider>
    </>
  );
};

export default MyApp;
