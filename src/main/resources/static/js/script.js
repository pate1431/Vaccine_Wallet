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