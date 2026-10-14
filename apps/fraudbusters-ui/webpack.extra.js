const webpack = require('webpack');
const SentryWebpackPlugin = require('@sentry/webpack-plugin');

module.exports = {
    plugins: [
        new webpack.IgnorePlugin({
            resourceRegExp: /^\.\/locale$/,
            contextRegExp: /moment$/,
        }),
        ...(process.env.SENTRY_AUTH_TOKEN
            ? [
                  new SentryWebpackPlugin({
                      authToken: process.env.SENTRY_AUTH_TOKEN,
                      org: 'vality-dev',
                      project: 'fraudbusters',
                      include: './dist',
                      ignore: ['node_modules', 'webpack.config.js'],
                  }),
              ]
            : []),
    ],
};
