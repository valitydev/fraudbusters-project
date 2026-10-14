"""Package exactly the filtered upstream Dockerfile's COPY inputs."""
from pathlib import Path
import shlex
import shutil
import sys
import zipfile

app, destination = sys.argv[1:]
target = Path("apps") / app / "target"
dockerfile = target / "Dockerfile"
text = dockerfile.read_text()
inputs = set()
for line in text.splitlines():
    if line.startswith("COPY ") or line.startswith("ADD "):
        parts = shlex.split(line)[1:]
        while parts and parts[0].startswith("--"):
            option = parts.pop(0)
            if not option.startswith(("--chown=", "--chmod=")):
                raise SystemExit("Unsupported COPY option: " + option)
        for pattern in parts[:-1]:
            if pattern.startswith("/") or ".." in Path(pattern).parts:
                raise SystemExit("Unsafe image source: " + pattern)
            matches = list(target.glob(pattern))
            if not matches:
                raise SystemExit("Missing image source: " + pattern)
            inputs.update(matches)
if not any(p.suffix == ".jar" and "BOOT-INF/classes/" in zipfile.ZipFile(p).namelist() for p in inputs if p.is_file()):
    raise SystemExit("No executable Spring Boot JAR in Docker COPY inputs")
output = Path(destination)
output.mkdir(parents=True, exist_ok=False)
shutil.copy2(dockerfile, output / "Dockerfile")
for source in sorted(inputs):
    to = output / source.relative_to(target)
    to.parent.mkdir(parents=True, exist_ok=True)
    if source.is_dir():
        shutil.copytree(source, to, dirs_exist_ok=True)
    else:
        shutil.copy2(source, to)
print(f"Packaged {app} with its upstream runtime Dockerfile")
