docker build -t my/app .
docker run --network mynetwork --network-alias appservice -p 8080:8080 my/app
pause