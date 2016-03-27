/**
 * Created by alexandros on 3/27/16.
 */

const connect = require('connect');
const serveStatic = require('serve-static');
connect().use(serveStatic(__dirname + '/public')).listen(8080, function() {
  console.log('Server running on 8080...');
});
