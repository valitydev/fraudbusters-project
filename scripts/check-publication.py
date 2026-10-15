#!/usr/bin/env python3
"""Do not let skipped or failed publishers hide behind successful build checks."""
import json
import os


def failures(needs, event, ref_name, default_branch):
    errors = []
    for job in ["affected", "checks"]:
        if needs[job]["result"] != "success":
            errors.append(job + " did not succeed")
    if errors:
        return errors
    outputs = needs["affected"]["outputs"]
    release = event in {"push", "workflow_dispatch"} and ref_name == default_branch
    required = {
        "java-images": release and bool(json.loads(outputs["java_apps"])),
        "ui-image": release and outputs["ui_changed"] == "true",
        "publish-packages": release and bool(json.loads(outputs["publish"])),
    }
    for job, selected in required.items():
        result = needs[job]["result"]
        if selected and result != "success":
            errors.append(job + " was selected but finished as " + result)
        elif not selected and result not in {"success", "skipped"}:
            errors.append(job + " finished as " + result)
    return errors


if __name__ == "__main__":
    errors = failures(json.loads(os.environ["NEEDS_JSON"]), os.environ["EVENT"],
                      os.environ["REF_NAME"], os.environ["DEFAULT_BRANCH"])
    if errors:
        raise SystemExit("\n".join(errors))
    print("All selected publishers succeeded; unselected groups may be skipped.")
