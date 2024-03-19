var express = require('express'),
    app = express(),
    server = require('http').createServer(app),
    users = [];
const fs = require('fs');
const path = require("path");

const io = require('socket.io')(server)

//specify the html we will use
app.use('/', express.static(__dirname + '/www'));

//bind the server to the 80 port
//server.listen(3000);//for local test
server.listen(process.env.PORT || 3001);//publish to heroku

io.sockets.on('connection', function(socket) {
    console.log('a user connected');

    //user leaves
    socket.on('disconnect', function() {
        console.log('a user disconnected');
        if (socket.nickname != null) {
            //users.splice(socket.userIndex, 1);
            users.splice(users.indexOf(socket.nickname), 1);
            socket.broadcast.emit('system', socket.nickname, users.length, 'logout');
        }
    });

    // on mousedown
    socket.on('mouse-down', function(data) {
        console.log("received mousedown event, start emit mousedown event");
        socket.broadcast.emit('mouse-down', data);
    });
    // on mouseup
    socket.on('mouse-up', function(data) {
        console.log("received mouse-up event, start broadcast mouse-up event");
        socket.broadcast.emit('mouse-up', data);
    });
    // on mousemove
    socket.on('mouse-move', function(data) {
        console.log('mousemove event:', data);
        socket.broadcast.emit('mouse-move', data);
    });
    // // on click
    // socket.on('mouse-click', function(data) {
    //     console.log('click event:', data);
    //     socket.broadcast.emit('mouse-click', data);
    // });

    //new image get
    socket.on('image', function(imgData, color) {
        console.log('image received');
        //socket.broadcast.emit('newImg', socket.nickname, imgData, color);
        var savePath = path.join(__dirname, "www",'screen.jpeg');
        fs.writeFile(savePath, imgData, function (err) {
            if (err) {
                console.log(err);
            } else {
                var pathWithTime = "screen.jpeg?t=" + new Date().getTime();
                console.log('image received, updateImage: ' + pathWithTime);
                socket.broadcast.emit('updateImage', socket.nickname, pathWithTime, color);
            }
        });
    });
});
