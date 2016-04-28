while true; do
        sleep 1;
        FILE="${1%.*}"
        LOG="$FILE.log"
		REF="$FILE.aux"
        if [ $1 -nt $LOG ]; then
                pdflatex -halt-on-error $1; bibtex $REF;
        fi;
done
