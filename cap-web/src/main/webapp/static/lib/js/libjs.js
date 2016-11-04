require.config({
  //all js setting
  paths : {
    'json3' : 'lib/js/json3.min',
    'socketio' : 'lib/js/socket.io',
    'jquery' : 'http://code.jquery.com/jquery-1.11.3.js',
    'jquery-migrate-1' : 'http://code.jquery.com/jquery-migrate-1.4.1.js',
    'jquery-ui' : 'lib/js/jquery/ui/js/jquery-ui-1.10.4.custom.min',
    'jqgrid-i18n' : 'lib/js/jquery/plugin/jquery.jqGrid-4.5.4/js/i18n/grid.locale-tw',
    'jqgrid' : 'lib/js/jquery/plugin/jquery.jqGrid-4.5.4/js/jquery.jqGrid.min',
    'validate-i18n' : 'lib/js/jquery/plugin/formValidator/languages/jquery.validationEngine-zh_TW',
    'validate' : 'lib/js/jquery/plugin/formValidator/jquery.validationEngine',
    'fileupload' : 'lib/js/jquery/plugin/ajaxfileupload/ajaxfileupload',
    'blockui' : 'lib/js/jquery/plugin/blockUI/jquery.blockUI',
    'underscore' : 'lib/js/backbone/underscore',
    'backbone' : 'lib/js/backbone/backbone',
    'sceditor' : 'lib/js/jquery/plugin/sceditor/jquery.sceditor.xhtml.min',
    'ifvisible' : 'lib/js/ifvisible/ifvisible', /* idle handle */
    'timer' : 'lib/js/timer/jquery.timer', /* timer handle */
    'fullcalendar' : 'lib/js/calendar/fullcalendar'
  },
  shim : {
    'blockui' : [ 'jquery' ],
    'jquery-ui' : [ 'jquery' ],
    'jqgrid-i18n' : [ 'jquery' ],
    'jqgrid' : [ 'jquery-ui', 'jqgrid-i18n' ],
    'validate-i18n' : [ 'jquery' ],
    'validate' : [ 'jquery', 'validate-i18n' ],
    'fileupload' : [ 'jquery' ],
    'backbone' : [ 'underscore', 'jquery' ],
    'sceditor' : [ 'jquery' ],
    'ifvisible' : [ 'jquery' ],
    'timer' : [ 'jquery' ],
    'fullcalendar' : [ 'jquery' ],
    'jquery-migrate-1' : [ 'jquery' ]
  }
});

define('libjs', [ 'json3', 'socketio', 'jquery', 'blockui', 'jquery-ui', 'jqgrid-i18n', 'jqgrid', 'validate-i18n', 'validate', 'fileupload', 'underscore', 'backbone', 'sceditor', 'ifvisible',
    'timer', 'fullcalendar' ], function() {
  window.ifvisible = require.s.contexts._.defined.ifvisible;
  // $.holdReady(true); // enable at common.js
  logDebug('lib init');
});
