var namespace = (function () {
    // defined within the local scope
    var privateMethod1 = function () { console.log('privateMethod1'); }
    var privateMethod2 = function () { console.log('privateMethod2'); }
    var privateProperty1 = 'foobar';
    return {
        // the object literal returned here can have as many
        // nested depths as you wish, however as mentioned,
        // this way of doing things works best for smaller,
        // limited-scope applications in my personal opinion
        publicMethod1: privateMethod1,
        //nested namespace with public properties
        properties:{
            publicProperty1: privateProperty1
        },
        //another tested namespace
        utils:{
            publicMethod2: privateMethod2
        }
    }
})();

var a=new namespace.publicMethod1();