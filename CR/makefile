FILENAME=cr
EXT_TO_REMOVE=*.aux *.log *.dvi *.ps *.out *.toc *.lot *.lof *.blg *.bbl
EMACS_DIRECTORY=$(HOME)/.emacs.d/elpa/
ORG=$(shell ls $(EMACS_DIRECTORY) | grep -i "org-" | head -1)/org.el

all: ${FILENAME}.pdf clean

${FILENAME}.pdf: ${FILENAME}.ps
	ps2pdf ${FILENAME}.ps ${FILENAME}.pdf

${FILENAME}.ps: ${FILENAME}.dvi
	dvips	${FILENAME}.dvi -o ${FILENAME}.ps

${FILENAME}.dvi: ${FILENAME}.tex ${FILENAME}.aux
	latex -shell-escape ${FILENAME} \
	latex -shell-escape ${FILENAME}

${FILENAME}.aux ${FILENAME}.toc: ${FILENAME}.tex
	latex -shell-escape ${FILENAME}

clean:
	rm $(EXT_TO_REMOVE) 2> /dev/null || true
