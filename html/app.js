var filepathInput = $("#filepath");
var scoreButton = $("#score");

scoreButton.click(function() {
    var filepath = filepathInput.val();

    if(filepath == "") {
        alert("Please enter a file path");
        return
    }

    var dfd = fetchScores(filepath);

    dfd.then(function(result) {
        populateInfo(result);
        populateText(result);
    });
});

$("a.examples").click(function(e) {
    e.preventDefault();
    var text = $(this).text();
    filepathInput.val(text);
});

function fetchScores(path) {
    path = path.trim("/")
    var url = "/nlp/" + path + "/?detailed=true";
    return $.ajax({
       url: url,
       dataType: 'JSON',
       method: 'GET',
    });
}

function populateInfo(result) {
    // populate the info boxes
    var scores = result.score;
    scores.forEach(function(score, i) {
        var element = $("td[data-n=" + (5-i) + "]");
        element.text(score);
    });

    $("#time").text(result.time + " ms");
}

function populateText(result) {
    if(result.detail) {
        $("#text").text(result.detail.text);
    }
}
