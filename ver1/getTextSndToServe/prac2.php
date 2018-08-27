<?php
$file_path = "../../../home/ubuntu/NN/uploads/";
echo "path: $file_path";

// Add the original filename to our target path.  
//Result is "uploads/filename.extension" */
$file_path = $file_path . basename( $_FILES['uploaded_file']['name']);
$output;

if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
    echo "The file upload success\n>";
        exec("python3 /home/ubuntu/NN/test_on_server.py > /dev/null &");
        echo "OK";

} else{
    echo "There was an error uploading the file, please try again!";
}

?>
