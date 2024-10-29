package cmd

import (
	"fmt"
	"net/http"
	"strings"
	"time"

	"github.com/spf13/cobra"
)

var (
	slackpostScansCmd = &cobra.Command{
		Use:   "scans",
		Short: "Make a Slack post for successful or failed scans",
		Long:  "",
		Run:   executeSlackpostCmd,
	}
	result string
)

func init() {
	slackpostScansCmd.PersistentFlags().StringVarP(&result, "result", "r", "Failed", "states the scan was a success")

	slackpostScansCmd.MarkPersistentFlagRequired("result")

	slackpostCmd.AddCommand(slackpostScansCmd)
}

func executeSlackpostCmd(cmd *cobra.Command, args []string) {
	var err error
	var resp *http.Response
	fmt.Printf("Distribution For Galasa - Slack %s Scan Result Report\n", result)

	linkToScanResults := "https://ibmets.whitesourcesoftware.com/Wss/WSS.html#!project;id=5501276"

	content := fmt.Sprintf("Distribution for Galasa scan %s:\n\nPlease see %s for details.", result, linkToScanResults)

	client := http.Client{
		Timeout: time.Second * 30,
	}

	body := fmt.Sprintf("{\"text\":\"%s\"}", content)

	resp, err = client.Post(slackWebhook, "application/json", strings.NewReader(body))
	if err != nil {
		fmt.Println(err.Error())
	} else {
		fmt.Printf("Response is: %s", resp.Status)
	}

}
