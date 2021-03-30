const wsProto = (window.location.protocol === 'https:') ? 'wss:' : 'ws:';
const wsBase = `${wsProto}//${window.location.hostname}:${window.location.port}`;

window.onload = function () {
    $(".alert").hide()
    let myForm = document.getElementById('myform');
    myForm.addEventListener('submit', function (event) {
        event.preventDefault();
        let formData = new FormData(myForm), result = {};

        for (let entry of formData.entries()) {
            result[entry[0]] = entry[1];
        }
        result = JSON.stringify(result)
        // console.log(result);

        let xhr = new XMLHttpRequest();

        xhr.open(myForm.method, myForm.action, true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        xhr.send(result);
        $(".alert").show()
    });

    let ws = new WebSocket(`${wsBase}/kafka`);
    ws.onmessage = function (event) {
        let data = JSON.parse(event.data);
        console.log(data)
    };

    /*window.setInterval(function () {
        ws.send(JSON.stringify({
            driver: uuid,
            lngLat: marker.getLngLat(),
            status: status,
            rider: rider
        }));

    }, 500);*/
}

