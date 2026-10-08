# Baseline npm audit (frontend, Node 16.20.2 / npm 8.19.4)

Summary: {'info': 0, 'low': 6, 'moderate': 54, 'high': 39, 'critical': 10, 'total': 109}

| package | severity | range | fix available |
|---|---|---|---|
| @ampproject/toolbox-core | low | 2.0.0-alpha.0 - 2.6.0 | True |
| @ampproject/toolbox-optimizer | high | 2.0.0-alpha.0 - 2.10.1 | next@16.4.0 (major) |
| @ampproject/toolbox-validator-rules | low | <=2.5.4 | True |
| @babel/core | low | <=7.29.0 | next@16.4.0 (major) |
| @babel/helpers | moderate | <7.26.10 | True |
| @babel/runtime | moderate | <7.26.10 | next@16.4.0 (major) |
| @babel/traverse | critical | <7.23.2 | True |
| @next/react-dev-overlay | critical | 9.3.7-canary.3 - 12.0.1 | next@16.4.0 (major) |
| adjust-sourcemap-loader | high | 0.1.0 - 2.0.0 | next@16.4.0 (major) |
| ajv | moderate | <6.14.0 | True |
| ansi-regex | high | 5.0.0 | True |
| anymatch | moderate | 1.2.0 - 2.0.0 | next@16.4.0 (major) |
| argparse | moderate | 1.0.0 - 1.0.10 | True |
| axios | high | <=0.32.0 | axios@1.20.0 (major) |
| bn.js | moderate | >=5.0.0 <5.2.3 || <4.12.3 | True |
| brace-expansion | high | <=1.1.20 | True |
| braces | high | * | next@16.4.0 (major) |
| browserify-sign | high | 2.6.0 - 4.2.1 | True |
| browserslist | high | <=4.28.6 | next@16.4.0 (major) |
| chokidar | high | 1.3.0 - 2.1.8 | next@16.4.0 (major) |
| cipher-base | critical | <=1.0.4 | True |
| color-string | moderate | <1.5.5 | True |
| core-js-compat | high | 3.6.0 - 3.25.0 | True |
| cross-fetch | low | 2.0.0 - 2.2.3 || 3.0.0 - 3.0.5 | next@16.4.0 (major) |
| css | moderate | >=2.2.2 | True |
| css-declaration-sorter | moderate | <=5.1.2 | True |
| css-loader | moderate | 0.15.0 - 4.3.0 | next@16.4.0 (major) |
| css-select | high | <=3.1.0 | True |
| cssnano | moderate | <=4.1.11 | next@16.4.0 (major) |
| cssnano-preset-default | moderate | <=4.0.8 | next@16.4.0 (major) |
| cssnano-preset-simple | moderate | <=1.2.2 || 1.3.1 | next@16.4.0 (major) |
| cssnano-simple | moderate | <=1.2.2 | next@16.4.0 (major) |
| cssnano-util-raw-cache | moderate | * | True |
| debug | low | 4.0.0 - 4.3.0 | True |
| decode-uri-component | high | <=0.4.1 | True |
| elliptic | critical | * | True |
| es5-ext | low | 0.10.1 - 0.10.62 | True |
| follow-redirects | high | <=1.15.11 | axios@1.20.0 (major) |
| glob-parent | high | 4.0.0 - 5.1.1 | True |
| icss-utils | moderate | <=4.1.1 | next@16.4.0 (major) |
| is-svg | high | 2.1.0 - 4.2.2 | True |
| js-yaml | high | <=3.15.2 | True |
| json5 | high | <1.0.2 || >=2.0.0 <2.2.2 | True |
| loader-utils | critical | <=1.4.1 || 2.0.0 - 2.0.3 | next@16.4.0 (major) |
| lodash | high | <=4.17.23 | True |
| marked | high | <=4.0.9 | marked@18.1.0 (major) |
| micromatch | high | <=4.0.7 | next@16.4.0 (major) |
| mini-css-extract-plugin | moderate | <=0.9.0 | next@16.4.0 (major) |
| minimatch | high | <=3.1.3 | True |
| minimist | critical | 1.0.0 - 1.2.5 | True |
| next | critical | 0.9.9 - 15.5.12 | next@16.4.0 (major) |
| node-fetch | high | <=2.6.6 | next@16.4.0 (major) |
| nth-check | high | <2.0.1 | True |
| object-path | high | <=0.11.7 | next@16.4.0 (major) |
| path-parse | moderate | <1.0.7 | True |
| pbkdf2 | critical | <=3.1.6 | True |
| picomatch | high | <=2.3.1 | True |
| postcss | high | <=8.5.22 | next@16.4.0 (major) |
| postcss-calc | moderate | 4.1.0 - 10.0.2 | True |
| postcss-colormin | moderate | <=4.0.3 | True |
| postcss-convert-values | moderate | <=4.0.1 | True |
| postcss-discard-comments | moderate | <=4.0.2 | True |
| postcss-discard-duplicates | moderate | 1.1.0 - 4.0.2 | True |
| postcss-discard-empty | moderate | 1.1.0 - 4.0.1 | True |
| postcss-discard-overridden | moderate | <=4.0.1 | True |
| postcss-merge-longhand | moderate | <=4.0.11 | True |
| postcss-merge-rules | moderate | <=7.0.4 | True |
| postcss-minify-font-values | moderate | <=4.0.2 | True |
| postcss-minify-gradients | moderate | <=4.0.2 | True |
| postcss-minify-params | moderate | <=4.0.2 | True |
| postcss-minify-selectors | moderate | <=7.0.4 | True |
| postcss-modules-extract-imports | moderate | <=2.0.0 | True |
| postcss-modules-local-by-default | moderate | <=4.0.5 | True |
| postcss-modules-scope | moderate | <=3.2.0 | True |
| postcss-modules-values | moderate | <=4.0.0-rc.5 | next@16.4.0 (major) |
| postcss-normalize-charset | moderate | <=4.0.1 | True |
| postcss-normalize-display-values | moderate | <=4.0.2 | True |
| postcss-normalize-positions | moderate | <=4.0.2 | True |
| postcss-normalize-repeat-style | moderate | <=4.0.2 | True |
| postcss-normalize-string | moderate | <=4.0.2 | True |
| postcss-normalize-timing-functions | moderate | <=4.0.2 | True |
| postcss-normalize-unicode | moderate | <=4.0.1 | True |
| postcss-normalize-url | moderate | 1.1.0 - 4.0.1 | True |
| postcss-normalize-whitespace | moderate | <=4.0.2 | True |
| postcss-ordered-values | moderate | <=4.1.2 | True |
| postcss-reduce-initial | moderate | <=4.0.3 | True |
| postcss-reduce-transforms | moderate | <=4.0.2 | True |
| postcss-safe-parser | moderate | <=4.0.2 | next@16.4.0 (major) |
| postcss-selector-parser | moderate | <7.1.6 | True |
| postcss-svgo | high | <=5.0.0-rc.2 | True |
| postcss-unique-selectors | moderate | <=4.0.1 | True |
| readdirp | moderate | 2.2.0 - 2.2.1 | next@16.4.0 (major) |
| resolve-url-loader | high | 0.0.1-experiment-postcss || 3.0.0-alpha.1 - 4.0.0 | next@16.4.0 (major) |
| semver | high | 2.0.0-alpha - 5.7.1 || 6.0.0 - 6.3.0 || 7.0.0 - 7.5.1 | True |
| serialize-javascript | high | <=7.0.2 | next@16.4.0 (major) |
| sha.js | critical | <=2.4.11 | True |
| shell-quote | critical | <=1.8.4 | next@16.4.0 (major) |
| source-map-resolve | moderate | >=0.5.1 | True |
| sprintf-js | moderate | * | True |
| ssri | high | 5.2.2 - 6.0.1 || 7.0.0 - 7.1.0 | True |
| styled-jsx | high | 3.0.0 - 5.0.0-beta.7 | next@16.4.0 (major) |
| stylehacks | moderate | <=7.0.4 | True |
| svgo | high | 1.0.0 - 2.8.3 | True |
| terser | high | <4.8.1 | next@16.4.0 (major) |
| terser-webpack-plugin | high | <=5.3.16 | next@16.4.0 (major) |
| watchpack | high | 1.7.2 - 1.7.5 | next@16.4.0 (major) |
| watchpack-chokidar2 | high | * | next@16.4.0 (major) |
| webpack | high | 4.0.0-alpha.0 - 5.1.0 | next@16.4.0 (major) |
| y18n | high | 4.0.0 | True |
