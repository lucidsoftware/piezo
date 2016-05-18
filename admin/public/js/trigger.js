
            function deleteTrigger(triggerUrl, redirectUrl) {
                var deleteRequest = new XMLHttpRequest();
                deleteRequest.onreadystatechange = function() {
                    if (deleteRequest.readyState === 4) {
                        if (deleteRequest.status === 200) {
                            window.location.assign(redirectUrl);
                        } else {
                            $('#deleteError').show();
                        }
                    }
                };
                deleteRequest.open("DELETE", triggerUrl, true);
                deleteRequest.send(null);
            }
            function clearTrigger(triggerUrl) {
                var patchRequest = new XMLHttpRequest();
                patchRequest.onreadystatechange = function() {
                    if (patchRequest.readyState === 4) {
                        if (patchRequest.status === 200) {
                            //TODO: show confirmation message
                        } else {
                            $('#deleteError').show();
                        }
                    }
                };
                patchRequest.open("PATCH", triggerUrl, true);
                patchRequest.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
                patchRequest.send(JSON.stringify({state: "NORMAL"}));
            }