const gulp = require('gulp');
const babel = require('gulp-babel');
const resolveDependencies = require('gulp-resolve-dependencies');
const concat = require('gulp-concat');
const webpack = require('webpack');

gulp.task('transpile', function() {
  return gulp.src('src/**')
  .pipe(babel({
      presets: ['es2015']
    }))
  .pipe(gulp.dest('dist'));
});

gulp.task('bundle', function() {
  gulp.src('./dist/index.js')
  .pipe(browserify({
      insertGlobals: true,
      debug: !gulp.env.production
    }))
  .pipe(gulp.dest('./'))
});

gulp.task('bundle', function() {
  gulp.src(['./dist/*.js'])
  .pipe(resolveDependencies({
      pattern: /\* @requires [\s-]*(.*\.js)/g
    }))
  .on('error', function(err) {
      console.log(err.message);
    })
  .pipe(concat('adblocker-utils.js'))
  .pipe(gulp.dest('./'))
});

gulp.task('webpack', function() {
  const config = {
    entry: './src/index.js',
    output: {
      path: __dirname + '/build',
      filename: 'adblocker-utils.js'
    },
    module: {
      loaders: [
        { test: require.resolve('./src/index.js'), loader: 'expose?$!expose?AdblockerUtils' },
        { test: /\.js?$/, loader: 'babel', query: {presets: ['es2015']} }
      ]
    }
  };
  webpack(config).run(function(err, stats) {
    if (err) {
      console.log('Error', err);
    } else {
      console.log(stats.toString());
    }
    //done();
  });
});

gulp.task('default', ['transpile', 'webpack']);
