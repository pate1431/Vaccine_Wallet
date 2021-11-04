
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