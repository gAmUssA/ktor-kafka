window.onload = function () {
    let wsUrl = "ws://localhost:8080/kafka";

    let ws = new WebSocket(wsUrl);
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

