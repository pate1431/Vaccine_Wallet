/*
function loadData(username)
{
    if(document.getElementById("database"+username).innerHTML=="")
    {
        fetch('http://localhost:8080/admin/database'+username)
    }
}*/
function getAccess(id)
{
    console.log(id)
    fetch('http://localhost:8080/admin/database/'+id)  // fetch data via our HomeController
        .then(data => data.json()) // JSONifythe data returned
        .then(function(data)
        {
            console.log(data.toString())

        });
}
function verify(){
    console.log("verify clicked")

    let password = document.forms['form']['password'].value;
    let vPassword = document.forms['form']['verifyPassword'].value;
    if(password = "") {
        alert("Hey Enter passwrod please");
        return false;
    }
    else if(vPassword=""){
        alert("Hey veridy pass");
        return false;
    }
}