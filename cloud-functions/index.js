const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions

// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.sendAlerts = functions.database
.ref('/users/{uid}/status')
.onWrite(event => {
    const uid = event.params.uid
    const root = event.data.ref.root
    const alert = event.data.val()
    var alertpriority = {
                            priority: "normal",
                            timeToLive: 60 * 60 * 24
                        };
    if(alert === "Green")
    {
        return true;
    }
    else if(alert === "Red")
    {
        alertpriority = {
            priority: "high",
            timeToLive: 60 * 60 * 24
        };
    }
    else
    {
        alertpriority = {
            priority: "normal",
            timeToLive: 60 * 60 * 24
        };
    }
    var contactreg = ""
    console.log("uid : "+uid)
    return root.child(`/usercontact/${uid}/primarycontactreg`).once('value')
        .then(snap => {
            contactreg = snap.val()
            return root.child(`/users/${uid}/displayname`).once('value')
            }).then(snap => {
                    const displayname = snap.val()
                    const payload = {
                        notification: {
                        title:  alert + ' Alert!',
                        body: displayname + ' has raised ' + alert + ' Alert'
                        },
                        data: {
                            title:  alert + ' Alert!',
                            body: displayname + ' has raised ' + alert + ' Alert',
                            senderid: String(uid) 
                        }
                    };
                    return admin.messaging().sendToDevice(contactreg, payload, alertpriority)            
                    }).then(response => {
                                // See the MessagingDevicesResponse reference documentation for
                                // the contents of response.
                                console.log("Successfully sent message:", response);
                                return;
                            })
                      .catch(error => {
                                console.log("Error sending message:", error);
                            });
});