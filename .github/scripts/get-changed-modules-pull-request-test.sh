#!/usr/bin/env bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
cd "${BASEDIR}"

REPO_ROOT=$(cd "${BASEDIR}/../.." ; pwd)

mkdir -p $BASEDIR/temp
rm -fr $BASEDIR/temp/*output.txt


if [[ "$(uname -o)" == "Darwin" ]]; then
    echo "You are trying to run this test script on Mac, which doesn't support associative arrays or the declare -A syntax."
    echo "Switch to linux, perhaps inside a docker container to test more"
    echo "like this:"
    echo "docker run -it -v $BASEDIR/mnt/test ubuntu:latest /mnt/test/get-changed-modules-pull-request-test.sh"
    exit 1
fi

# Every flag the script under test writes out.
ALL_FLAGS="BUILDUTILS PLATFORM WRAPPING GRADLE MAVEN FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

failure_count=0
case_count=0

# Runs the script under test against a list of changed paths, and checks
# that exactly the expected flags are true and all the others are false.
#
# Parameters:
#   $1 : A name for the test case, which describes what is being tested and expected.
#   $2 : A space-separated list of changed file paths, as a Pull Request would report them.
#   $3 : A space-separated list of flag prefixes (eg: "CLI DOCS") expected to be true.
#        Use ALL to expect every flag to be true, or NONE to expect every flag to be false.
function assert_flags_for_changed_paths() {
    local case_name=$1
    local changed_paths=$2
    local expected_true_flags=$3

    case_count=$((case_count + 1))

    local input_file=$BASEDIR/temp/${case_name}-input.txt
    local output_file=$BASEDIR/temp/${case_name}-output.txt

    printf '%s\n' $changed_paths > $input_file
    rm -f $output_file

    $BASEDIR/get-changed-modules-pull-request.sh --test --test-input-file $input_file --test-output-file $output_file > /dev/null

    if [[ "$expected_true_flags" == "ALL" ]]; then
        expected_true_flags=$ALL_FLAGS
    elif [[ "$expected_true_flags" == "NONE" ]]; then
        expected_true_flags=""
    fi

    local case_failed="false"
    for flag in $ALL_FLAGS; do
        local expected_value="false"
        for expected_flag in $expected_true_flags; do
            if [[ "$expected_flag" == "$flag" ]]; then
                expected_value="true"
            fi
        done

        local actual_value=$(grep "^${flag}_CHANGED=" $output_file | cut -d= -f2)
        if [[ "$actual_value" != "$expected_value" ]]; then
            echo "FAILED: $case_name: ${flag}_CHANGED should be '$expected_value' but was '$actual_value'. Changed paths: $changed_paths"
            case_failed="true"
        fi
    done

    if [[ "$case_failed" == "true" ]]; then
        failure_count=$((failure_count + 1))
    else
        echo "Passed: $case_name"
    fi
}

#-----------------------------------------------------------------------------------------
# Module changes cascade to every dependent module.
#-----------------------------------------------------------------------------------------
function test_module_changes_cascade_to_dependents() {
    assert_flags_for_changed_paths "platform_change_sets_every_module_except_buildutils" \
        "modules/platform/a modules/platform/b" \
        "PLATFORM WRAPPING GRADLE MAVEN FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "buildutils_change_sets_framework_and_everything_downstream" \
        "modules/buildutils/a" \
        "BUILDUTILS FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "wrapping_change_sets_framework_and_everything_downstream" \
        "modules/wrapping/a" \
        "WRAPPING FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "gradle_change_sets_maven_framework_and_everything_downstream" \
        "modules/gradle/a" \
        "GRADLE MAVEN FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "maven_change_sets_framework_and_everything_downstream" \
        "modules/maven/a" \
        "MAVEN FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "framework_change_sets_extensions_managers_obr_ivts_cli_and_docs" \
        "modules/framework/a" \
        "FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "galasa_boot_change_sets_framework_and_cli" \
        "modules/framework/galasa-parent/galasa-boot/a" \
        "FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "openapi_yaml_change_sets_framework_and_cli" \
        "modules/framework/galasa-parent/dev.galasa.framework.api.openapi/src/main/resources/openapi.yaml" \
        "FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "extensions_change_sets_obr_ivts_cli_and_docs" \
        "modules/extensions/a" \
        "EXTENSIONS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "managers_change_sets_obr_ivts_cli_and_docs" \
        "modules/managers/a" \
        "MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "obr_change_sets_ivts_cli_and_docs" \
        "modules/obr/a" \
        "OBR IVTS CLI DOCS"
}

#-----------------------------------------------------------------------------------------
# Leaf modules do not cascade upstream or sideways.
#-----------------------------------------------------------------------------------------
function test_leaf_module_changes_do_not_cascade() {
    assert_flags_for_changed_paths "cli_change_sets_only_cli_and_docs" \
        "modules/cli/a modules/cli/b" \
        "CLI DOCS"

    assert_flags_for_changed_paths "ivts_change_sets_only_ivts" \
        "modules/ivts/a" \
        "IVTS"

    assert_flags_for_changed_paths "docs_change_sets_only_docs" \
        "docs/a docs/b" \
        "DOCS"

    assert_flags_for_changed_paths "ivts_and_docs_changes_set_only_ivts_and_docs" \
        "modules/ivts/a docs/a" \
        "IVTS DOCS"
}

#-----------------------------------------------------------------------------------------
# Workflow file changes force the builds they govern.
#-----------------------------------------------------------------------------------------
function test_module_workflow_changes_force_that_module_and_dependents() {
    assert_flags_for_changed_paths "pr_managers_workflow_change_sets_managers_and_dependents" \
        ".github/workflows/pr-managers.yaml" \
        "MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "managers_workflow_change_sets_managers_and_dependents" \
        ".github/workflows/managers.yaml" \
        "MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "pr_platform_workflow_change_sets_platform_and_dependents" \
        ".github/workflows/pr-platform.yaml" \
        "PLATFORM WRAPPING GRADLE MAVEN FRAMEWORK EXTENSIONS MANAGERS OBR IVTS CLI DOCS"

    assert_flags_for_changed_paths "pr_ivts_workflow_change_sets_only_ivts" \
        ".github/workflows/pr-ivts.yaml" \
        "IVTS"

    assert_flags_for_changed_paths "pr_docs_workflow_change_sets_only_docs" \
        ".github/workflows/pr-docs.yaml" \
        "DOCS"
}

function test_shared_ci_file_changes_force_every_module() {
    local shared_files=(
        ".github/workflows/pull-requests.yaml"
        ".github/workflows/pushes.yaml"
        ".github/workflows/codeql-go.yml"
        ".github/workflows/codeql-java.yml"
        ".github/workflows/test-creds-store.yaml"
        ".github/workflows/snyk-security-scan.yaml"
        ".github/workflows/check-client-java-version.yaml"
        ".github/workflows/check-required-secrets-configured.yaml"
        ".github/workflows/releases.yaml"
        ".github/workflows/release-cli.yaml"
        ".github/scripts/get-changed-modules-pull-request.sh"
        ".github/common-files/settings.xml"
    )

    for shared_file in "${shared_files[@]}"; do
        local case_name="shared_ci_file_${shared_file//[^a-zA-Z0-9]/_}_change_sets_every_flag"
        assert_flags_for_changed_paths "$case_name" "$shared_file" "ALL"
    done
}

function test_every_workflow_file_is_classified() {
    # A workflow file that is neither module-specific nor shared would silently
    # build nothing, so every file in the workflows directory must set at least one flag.
    for workflow_path in $REPO_ROOT/.github/workflows/*; do
        local relative_path=".github/workflows/$(basename $workflow_path)"
        local case_name="workflow_file_${relative_path//[^a-zA-Z0-9]/_}_is_classified"
        local input_file=$BASEDIR/temp/${case_name}-input.txt
        local output_file=$BASEDIR/temp/${case_name}-output.txt

        case_count=$((case_count + 1))
        echo "$relative_path" > $input_file
        rm -f $output_file
        $BASEDIR/get-changed-modules-pull-request.sh --test --test-input-file $input_file --test-output-file $output_file > /dev/null

        local true_flag_count=$(grep "_CHANGED=true" $output_file | wc -l | xargs)
        if [[ "$true_flag_count" == "0" ]]; then
            echo "FAILED: $case_name: $relative_path sets no flags. Classify it as a module workflow or a shared CI file in get-changed-modules-pull-request.sh"
            failure_count=$((failure_count + 1))
        else
            echo "Passed: $case_name"
        fi
    done
}

#-----------------------------------------------------------------------------------------
# Paths that need no build set no flags.
#-----------------------------------------------------------------------------------------
function test_harmless_paths_set_no_flags() {
    assert_flags_for_changed_paths "issue_template_change_sets_no_flags" \
        ".github/ISSUE_TEMPLATE/bug_report.md" \
        "NONE"

    assert_flags_for_changed_paths "security_md_change_sets_no_flags" \
        ".github/SECURITY.md" \
        "NONE"

    assert_flags_for_changed_paths "dependabot_yml_change_sets_no_flags" \
        ".github/dependabot.yml" \
        "NONE"

    assert_flags_for_changed_paths "pull_request_template_change_sets_no_flags" \
        ".github/pull_request_template.md" \
        "NONE"

    assert_flags_for_changed_paths "root_level_renovate_json_change_sets_no_flags" \
        "renovate.json" \
        "NONE"

    assert_flags_for_changed_paths "tools_directory_change_sets_no_flags" \
        "tools/build-locally.sh" \
        "NONE"
}

test_module_changes_cascade_to_dependents
test_leaf_module_changes_do_not_cascade
test_module_workflow_changes_force_that_module_and_dependents
test_shared_ci_file_changes_force_every_module
test_every_workflow_file_is_classified
test_harmless_paths_set_no_flags

echo ""
echo "$case_count test cases run, $failure_count failed."
if [[ "$failure_count" != "0" ]]; then
    exit 1
fi
