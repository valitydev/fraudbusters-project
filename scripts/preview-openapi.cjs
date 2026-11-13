const { createRequire } = require('module');
const { resolve } = require('path');

const cliPackage = process.argv.splice(2, 1)[0];
const requireFromPackage = createRequire(resolve('package.json'));
const cliPath = requireFromPackage.resolve(`${cliPackage}/bin/cli.js`);
const handlebars = createRequire(cliPath)('handlebars');

// These preview CLIs load ReDoc from a hardcoded "latest" URL, but their
// window.Redoc.init integration requires the classic ReDoc 2 bundle.
// Override the template helper to keep the CLI's options and live reload intact.
handlebars.registerHelper('redocHead', function () {
  return this.redocHead
    .replace('/redoc@latest/bundles/', '/redoc@2.5.0/bundles/')
    .replace('/redoc/latest/bundles/', '/redoc/v2.5.0/bundles/');
});

process.argv[1] = cliPath;
process.argv.splice(2, 0, 'preview-docs');
require(cliPath);
