<?php
	header("Content-type:application/json");
	require_once 'connect.php';

	$sql = mysqli_query($con, "SELECT * FROM sample");
	$response = array();

	while($row = mysqli_fetch_assoc($sql))
	{
		array_push($response, array(
			'id' => $row['id'],
			'url' => $row['url']
		));
	}

	echo json_encode($response, JSON_UNESCAPED_UNICODE);
?>
