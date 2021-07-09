const functions = require("firebase-functions");
const admin = require('firebase-admin');
admin.initializeApp();

exports.observeDataChange = functions.database.ref().onWrite(function(change, context){
    const message = {
        data: {
            "notificationType": "dataChange"
            },
        topic: "guestorganizersuperology",
        };
    admin.messaging().send(message);
});