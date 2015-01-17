using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using Word = Microsoft.Office.Interop.Word;
using Office = Microsoft.Office.Core;
using Microsoft.Office.Tools.Word;
using System.Reflection;
using System.IO;
using System.Net.Http;
using Newtonsoft.Json;
using System.Windows.Forms;

namespace WordAddIn1
{
    public partial class MakedonskiLeksikon
    {
        String jsonurl = "http://manu.edu.mk:9003/word_lookup/";
       
        Office.CommandBarButton button;
        String selectedWord = String.Empty;
        String currentWord = String.Empty;
        String caption = "Дефиниција и превод";

        private MkThesarusPanel mk_thesarus_panel;
        private Microsoft.Office.Tools.CustomTaskPane myCustomTaskPane;
        bool text_selected = false;

        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
            Document vstoDoc = Globals.Factory.GetVstoObject(this.Application.ActiveDocument);
            vstoDoc.SelectionChange += new Microsoft.Office.Tools.Word.SelectionEventHandler(ThisDocument_SelectionChange);

            mk_thesarus_panel = new MkThesarusPanel();
            myCustomTaskPane = this.CustomTaskPanes.Add(mk_thesarus_panel, caption);
            myCustomTaskPane.Width = 250;
            myCustomTaskPane.DockPositionRestrict = Office.MsoCTPDockPositionRestrict.msoCTPDockPositionRestrictNoChange;
           
            Application.WindowBeforeRightClick +=
                       new Word.ApplicationEvents4_WindowBeforeRightClickEventHandler(application_WindowBeforeRightClick);

            Application.CustomizationContext = Application.ActiveDocument;
           
            Office.CommandBar commandBar = Application.CommandBars["Text"];
            button = (Microsoft.Office.Core.CommandBarButton)commandBar.Controls.Add(Microsoft.Office.Core.MsoControlType.msoControlButton, Missing.Value, Missing.Value, 1, true);
            button.Caption = caption;
            button.BeginGroup = true;
            button.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(MyButton_Click);
            button.Tag = "MYRIGHTCLICKBUTTON";

            
        }

        void ThisDocument_SelectionChange(object sender, Microsoft.Office.Tools.Word.SelectionEventArgs e)
        {
           selectedWord = String.Empty;
           Microsoft.Office.Interop.Word.Selection currentSelection = Application.Selection;
           if (currentSelection == null) return;
           if (currentSelection.Range == null) return;
           if (currentSelection.Range.Text == null) return;
           selectedWord = currentSelection.Range.Text.Trim();
           if (selectedWord.Length > 0) text_selected = true;
           if (text_selected && myCustomTaskPane.Visible)
           {
               currentWord = selectedWord;
               populateThesarus();
           }
        }

        //populates the thesarus for the current selected word
        //if there is nor word selected does nothing
        private void populateThesarus()
        {
            myCustomTaskPane.Visible = true;
            mk_thesarus_panel.Clear();
            mk_thesarus_panel.AddPanel("", "Пребарува...");
            try
            {
                if (selectedWord.Length == 0)
                    return;
                //open a connection to the web service
                var client = new HttpClient();
                var uri = new Uri(jsonurl);
                //get the json for the selected word and parse ot to a WordInformation Object
                String resp = client.GetStringAsync(uri + currentWord.Replace(" ", "%20")).Result;
                WordInformation wi = JsonConvert.DeserializeObject<WordInformation>(resp);
                //check what information is available and concatanete if there are more than 1 definition
                String type = "";
                if (wi.Type != null)
                    type = wi.Type;
                String leksikon = concatenateString(wi.Definitions); ;
                if (leksikon.Length > 0 || type.Length > 0)
                    leksikon = selectedWord + " " + type + System.Environment.NewLine + leksikon;
                String recnik = concatenateString(wi.Translations);
                String wikipedia = concatenateString(wi.Wikipedia_defs);
                //only display the pane wen there is some information available
                 if ( leksikon.Length == 0 &&
                     recnik.Length == 0 &&
                    wikipedia.Length == 0)
                {
                    //otherwise inform the user that there is no information
                    mk_thesarus_panel.Clear();
                    mk_thesarus_panel.AddPanel("", "Нема информации за:" + currentWord);
                    return;
                }
                //add the information to the pane, in separate groups
                Dictionary<String, String> data = new Dictionary<string, string>();
                data["Дефиниција"] = leksikon;
                data["Превод"] = recnik;
                data["Википедија"] = wikipedia;
                mk_thesarus_panel.Clear();
                foreach (String key in data.Keys)
                {
                    if (data[key].Length == 0)
                        continue;
                    mk_thesarus_panel.AddPanel(key, data[key]);
                }
            }
            catch (Exception e)
            {
                //if any excception occured treat it as if no information was avaible
                mk_thesarus_panel.Clear();
                mk_thesarus_panel.AddPanel("", "Нема информации за:" + currentWord);
            }
        }

        //gets called when the user clicks on the thesarus button in the right click menu
        private void MyButton_Click(Office.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            populateThesarus();
        }

        private String concatenateString(String[] defs) {
            //concatenates an array of strings, by inserting a sequency number beofre each string, and a newline at the ned of each string
            StringBuilder sb = new StringBuilder();
            if (defs == null || defs.Length == 0) return "";
            int idx = 1;
            if (defs.Length == 1)
                return defs[0];
            foreach (String def in defs)
            {
                sb.Append(idx++ + ". "+def + System.Environment.NewLine);
            }
            if (sb.Length > 0)
                sb.Remove(sb.Length - 1,1);
            return sb.ToString();
        }

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
        }


        public void application_WindowBeforeRightClick(Word.Selection selection, ref bool Cancel)
        {
           //fetch the currently selected word, if any
           Cancel = true;
           if (selectedWord.Length == 0 || text_selected) 
           {
               //get the current selection
               Microsoft.Office.Interop.Word.Selection currentSelection = Application.Selection;
               //if the current selection is a fetch it and trim any whitespaces
               if (currentSelection.Type == Word.WdSelectionType.wdSelectionNormal)
               {
                   selectedWord = currentSelection.Range.Text.Trim();
               }
               else
               {
                   //if the current selection is a part of a word select fetch the whole word
                   if (currentSelection.Type == Word.WdSelectionType.wdSelectionIP)
                   {
                       Microsoft.Office.Interop.Word.Range current_range = currentSelection.Range;
                       current_range.MoveStart(Word.WdUnits.wdWord, -1);
                       current_range.MoveEnd(Word.WdUnits.wdWord, 1);
                       selectedWord = current_range.Text.Trim();
                   }
               }
               text_selected = true;
            }
           currentWord = selectedWord;
            //if no word is slected hide the thesarus button, otherwise show it
           if (String.IsNullOrEmpty(selectedWord))
               SetCommandVisibility(caption, false);
           else
               SetCommandVisibility(caption, true);
           //show the right click menu
           Application.CommandBars["Text"].ShowPopup();
           selectedWord = "";
        }

        private void SetCommandVisibility(string name, bool visible)
        {
            Application.CustomizationContext = Application.ActiveDocument;
            Office.CommandBar commandBar = Application.CommandBars["Text"];
            commandBar.Controls[name].Visible = visible;
        }

        #region VSTO generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisAddIn_Startup);
            this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);
        }
        
        #endregion
    }
}
