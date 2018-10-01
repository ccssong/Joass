var express =require('express');
var multer = require('multer');
//var fs = require('file-system');
var PythonShell = require('python-shell');
const path = require('path');


let options = {
        mode: 'text',
        scriptPath: '/home/ubuntu/NN/'
};

var app = express();

var storage = multer.diskStorage({
        destination: function(req,file,cb){
                cb(null, '/home/ubuntu/NN/uploads/')
        },
        filename: function(req, file, cb){
                cb(null, file.originalname);
        }
})

var upload = multer({storage: storage})

app.get('/', function(req, res){
        res.send('Hello javascript  world');
});

app.post('/upload', upload.single('sound'),  function(req, res){
        console.log(req.header);
        console.log(req.file);
             
        const spawn = require("child_process").spawn;
        const pythonProcess = spawn('python3', ["/home/ubuntu/NN/test_on_server.py"]);
        
        const newDirect = "/var/www/html/";
        const file2read = "new.php"; 
        var fs = require('file-system');
    
        pythonProcess.stdout.on('data', function(data){
                console.log(data.toString());
                console.log("?");
                res.setHeader('Content-Type', 'text/plain');
                
                fs.readFile(path.join(newDirect,file2read), 'utf8', function(err, text){
                        if(err){
                                console.log("설마");
                                console.log(err.toStrong());
                        }
                        //else{
                                console.log("access");
                                console.log(text.toString());
                                //res.send(text.toString());
                        //}

                });
                res.send(data.toString());
        });
        pythonProcess.stderr.on('data', (data) => {
                res.setHeader('Content-Type', 'text/plain');
        });

});

const port = 3000;
const hostname = 'ec2-13-125-251-29.ap-northeast-2.compute.amazonaws.com';
var server = app.listen(port,hostname, function(err){
        var host = server.address().address;
        var port = server.address().port;

        console.log('앱은 http://%s:%s에서 작동중입니다.',host,port);
});
