
HANDOUT = handout
SRC = ${HANDOUT}.text

main:
	pandoc --css="http://homepages.cwi.nl/~storm/teaching/bb.css" -t html ${SRC} > ${HANDOUT}.html
	markdown2pdf -o ${HANDOUT}.pdf ${SRC}
