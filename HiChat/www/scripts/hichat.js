
window.onload = function() {
    var hichat = new HiChat();
    hichat.init();
};
var HiChat = function() {
    this.socket = null;
};
HiChat.prototype = {
    init: function() {
        var that = this;
        this.socket = io.connect();

        const image = document.getElementById('image');

        this.socket.on('updateImage', function(user, img, color) {
            that._updateImage(user, img, color);
        });

        // Get the image element
        let isMouseDown = false;

        // Add the dragstart event listener
        image.addEventListener('dragstart', function(event) {
            event.preventDefault();
        });

        // // Add the click event listener
        // image.addEventListener('click', function(event) {
        //     console.log("start to emit [click] event");
        //     that.socket.emit('mouse-click', {
        //         x: event.offsetX,
        //         y: event.offsetY,
        //         width: image.clientWidth,
        //         height: image.clientHeight
        //     });
        // });

        // Add the mousedown event listener
        image.addEventListener('mousedown', function(event) {
            isMouseDown = true;
            console.log("start to emit [mouse-down] event");
            that.socket.emit('mouse-down', {
                x: event.offsetX,
                y: event.offsetY,
                width: image.clientWidth,
                height: image.clientHeight
            });
        });

        // Add the mouseup event listener
        image.addEventListener('mouseup', function(event) {
            if (!isMouseDown) {
                console.log("start to emit [mouse-up] failed");
                return;
            }
            console.log("start to emit [mouse-up] event");
            that.socket.emit('mouse-up', {
                x: event.offsetX,
                y: event.offsetY,
                width: image.clientWidth,
                height: image.clientHeight
            });
            isMouseDown = false;
        });
        // Add the mouseleave event listener
        image.addEventListener('mouseleave', function(event) {
            if (!isMouseDown) {
                return;
            }
            console.log("start to emit [mouse-up-mouseleave] event");
            that.socket.emit('mouse-up', {
                x: event.offsetX,
                y: event.offsetY,
                width: image.clientWidth,
                height: image.clientHeight
            });
            isMouseDown = false;
        });
        // Add the mousemove event listener
        image.addEventListener('mousemove', function(event) {
            if (isMouseDown) {
                console.log("start to emit [mousemove] event, event.offsetX: " + event.offsetX + ", event.offsetY: " + event.offsetY);
                that.socket.emit('mouse-move', {
                    x: event.offsetX,
                    y: event.offsetY,
                    width: image.clientWidth,
                    height: image.clientHeight
                });
            }
        });
    },

    _updateImage: function(user, pathWithTime, color) {
        const image = document.getElementById('image');
        image.src = pathWithTime;
    }
};
