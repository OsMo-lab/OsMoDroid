all: dist

dist:
	tangram-bundle cinnabar-style-more-labels.yaml
	mv cinnabar-style-more-labels.zip dist/cinnabar-style-more-labels.zip

clean:
	rm -rf dist
	mkdir dist

tag:
	git tag  -m 'See CHANGELOG for details.' -a v`cat VERSION`
