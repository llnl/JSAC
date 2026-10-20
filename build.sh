
grab_repo() {
    if [ -d "$1" ]; then
        echo "Repository exists. Updating..."
        cd "$1" && git fetch
    else
        echo "Repository does not exist. Cloning..."
        git clone "$2" "$1"
        cd "$1"
    fi
    git checkout "$3"
    cd ..
}

cd vendor

grab_repo response https://github.com/llnl/response.git 1.0.2

grab_repo gnemcore https://github.com/llnl/gnemcore.git 1.3.2

grab_repo iris-ws https://github.com/llnl/iris-ws.git 2.20.3

grab_repo coda-calibration https://github.com/llnl/coda-calibration-tool.git 1.0.25

grab_repo fdsndata https://github.com/llnl/fdsndata.git 1.0.0

mvn clean install -c -DskipTests=true

cd ..

mvn clean install
