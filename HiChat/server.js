var express = require('express'),
    app = express(),
    server = require('http').createServer(app),
    users = [];
const fs = require('fs');
const path = require("path");

const io = require('socket.io')(server)
let globalSocket;

const multer = require('multer');

// Set storage engine
const storage = multer.diskStorage({
    destination: './www/uploads/',
    filename: function(req, file, cb) {
        cb(null, file.fieldname + '-' + Date.now() + path.extname(file.originalname));
    }
});

// Init upload，不检查文件类型
const upload = multer({
    storage: storage
}).single('apk');

// Upload image
app.post('/upload', (req, res) => {
    upload(req, res, (err) => {
        if (err) {
            console.log(err);
            res.send('error');
        } else {
            console.log("upload success");
            console.log(req.file);
            res.send('success');
            // 通过socket.io发送消息
            if (globalSocket) {
                globalSocket.broadcast.emit('apk-upload', {
                    name: req.file.filename,
                    path: '/uploads/' + req.file.filename
                });
            }
        }
    });
});




//specify the html we will use
app.use('/', express.static(__dirname + '/www'));

//bind the server to the 80 port
//server.listen(3000);//for local test
server.listen(process.env.PORT || 3001);//publish to heroku

io.sockets.on('connection', function(socket) {
    console.log('a user connected');
    globalSocket = socket;
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
    // apk upload
    socket.on('apk-upload', function(data) {
        console.log('apk-upload event:', data);
        socket.broadcast.emit('apk-upload', data);
    });

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
