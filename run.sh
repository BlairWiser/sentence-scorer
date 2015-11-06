export LMDIR=$HOME/models
export COLLECTIONS_UPLOAD_FOLDER=$HOME/upload

mkdir -p $COLLECTIONS_UPLOAD_FOLDER

lein ring server-headless

