# Build the Cloudflow documentation

SHELL_DIR := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
ROOT_DIR := ${SHELL_DIR}/..

antora_docker_image     := lightbend/antora-cloudflow-contrib-doc
antora_docker_image_tag := 2.3.4

work_dir := ${ROOT_DIR}/docs/target

all: build

local-preview: html-author-mode
	@echo "Access the documentation on http://localhost:8000"
	(cd target/; python3 -m http.server)


show:
	echo work dir: ${work_dir}
	echo ROOT_DIR: ${ROOT_DIR}

clean:
	rm -rf ${work_dir}

build: clean html all_javascaladocs print-site

get-version:
	# get latest tag
	$(eval version=$(shell sh -c "git for-each-ref refs/tags --sort=-taggerdate --format='%(refname)' --count=1 | sed 's|refs/tags/v||'"))

set-antora-versions: get-version
	yq e ".version = \"${version}\"" docs-source/docs/base-antora.yml > docs-source/docs/antora.yml
	yq e -i ".asciidoc.attributes.cloudflow-contrib-version = \"${version}\"" docs-source/docs/antora.yml

set-antora-versions-to-dev: get-version
	yq e ".version = \"dev\"" docs-source/docs/base-antora.yml > docs-source/docs/antora.yml
	yq e -i ".asciidoc.attributes.cloudflow-contrib-version = \"dev\"" docs-source/docs/antora.yml

html: clean
	docker run \
		-u $(shell id -u):$(shell id -g) \
		--privileged \
		-v ${ROOT_DIR}:/antora \
		--rm \
		-t ${antora_docker_image}:${antora_docker_image_tag} \
		--stacktrace \
		--cache-dir=/tmp/.cache/antora \
		docs/docs-source/site.yml
	@echo "Done"

html-author-mode: clean
	docker run \
		-u $(shell id -u):$(shell id -g) \
		-v ${ROOT_DIR}:/antora \
		--rm \
		-t ${antora_docker_image}:${antora_docker_image_tag} \
		--cache-dir=/tmp/.cache/antora \
		--stacktrace \
		docs/docs-source/author-mode-site.yml
	@echo "Done"

check-links:
	docker run \
		-v ${ROOT_DIR}:/antora \
		--rm \
		--entrypoint /bin/sh \
		-t lightbend/antora-doc:0.1.0 \
		--cache-dir=/tmp/.cache/antora \
		-c 'find docs/docs-source -name '*.adoc' -print0 | xargs -0 -n1 asciidoc-link-check -p -c docs/docs-source/asciidoc-link-check-config.json'

# Generate the ScalaDoc and the JavaDoc, for the old versions
all_scaladocs: get-version
	@echo "Building old java and scala docs"
	(./build-javascaladocs.sh v${version})

gh-pages: get-version
	@echo "Generating gh-pages additional content"
	touch target/.nojekyll
	sed "s/version/${version}/g" index.html > target/index.html

print-site:
	# The result directory with the contents of this build:
	@echo "${work_dir}"
