/**
 * Created by alexandros on 4/1/16.
 */

const gulp = require('gulp');

gulp.task('default', function() {
  console.log('im printing something');
  gulp.src([
    './node_modules/jquery/dist/jquery.js',
    './node_modules/moment/moment.js',
    './node_modules/adblocker-utils/build/adblocker-utils.js'
  ])
    .pipe(gulp.dest('./data/libs'));
});
