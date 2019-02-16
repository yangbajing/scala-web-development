window.utils = {
  parseSearch: function () {
    var tuples = window.location.search.slice(1).split('&');
    var search = [];
    tuples.forEach(function (item) {
      var tp = item.split('=');
      search.push({name: tp[0], value: tp[1]});
    });
    return search;
  }
};

$(function () {
  var search = window.utils.parseSearch();
  var $form = $('#form');

  function append(item) {
    console.log('append item', item);
    $form.append('<input type="hidden" name="' + item.name + '" value="' + item.value + '">');
  }

  search.forEach(append);
  // $form.on('submit', function(e) {
  //   e.preventDefault();
  //   var payload = search;
  //   $form.serializeArray().forEach(function(item) {
  //     payload[item.name] = item.value;
  //   });
  //   console.log('payload', payload);
  //
  //   axios.post('/auth/signin', {data: payload}).then(function(resp) {
  //     console.log(resp);
  //   });
  // });
});