FORMAT := png svg
SOURCEDIR := $(abspath $(CURDIR)/../../../docs/fraudo/content)
STYLE := plantuml-toolset/super-serious-style.isvg

all: plantuml-toolset

plantuml-toolset:
	git clone https://github.com/empayre/plantuml-toolset.git

update-toolset: plantuml-toolset
	cd $< && git pull

-include plantuml-toolset/wsd.mk