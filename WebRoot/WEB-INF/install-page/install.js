jQuery(function($) {

var left = $('#left');
var right = $("#right");

left.css({"padding-right": 0})
    .animate({"padding-right": 20, "opacity": 1}, 1000);

right.css({"padding-top": 80})
     .animate({"padding-top": 100, "opacity": 1});

$('form').submit(hidden);


$('[setto]').click(function() {
  var thiz = $(this);
  var v = thiz.attr("val");
  var to = thiz.attr("setto");
  $(to).val(v);
  console.log("set to", to, v);
  return false;
});


function hidden() {
  left.animate({"opacity": 0.1}, 5000);
  right.animate({"opacity": 0.1}, 5000);
}

});