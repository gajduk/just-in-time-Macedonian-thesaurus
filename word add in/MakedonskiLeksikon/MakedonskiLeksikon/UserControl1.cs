using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace WordAddIn1
{
    public partial class MkThesarusPanel : UserControl
    {
        public MkThesarusPanel()
        {
            InitializeComponent();
        }


        internal void AddPanel(string label_text, string txt_text)
        {
            this.AutoScroll = true;

            this.SuspendLayout();
            System.Windows.Forms.Label label = new System.Windows.Forms.Label();
            label.AutoSize = true;
            label.Location = new System.Drawing.Point(this.left_lbl_padding, this.y);
            label.Name = "lbl_"+label_text;
            label.Size = new System.Drawing.Size(this.width, this.height);
            this.y += this.height + this.padding;
            label.TabIndex = 0;
            label.Text = label_text;


            System.Windows.Forms.TextBox textBox = new System.Windows.Forms.TextBox();
            textBox.Location = new System.Drawing.Point(this.left_txt_padding, this.y);
            textBox.Multiline = true;
            textBox.Name = "txt_" + label_text;
            textBox.ReadOnly = true;
            textBox.Text = txt_text;
            textBox.Size = new System.Drawing.Size(this.width, this.height);
            textBox.Height = this.padding+textBox.Font.Height * (textBox.GetLineFromCharIndex(textBox.TextLength) + 1);
            this.y += textBox.Height + this.padding + padding_large;
            textBox.BackColor = System.Drawing.SystemColors.Info;
            textBox.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            textBox.TabIndex = 0;

            this.Controls.Add(label);
            this.Controls.Add(textBox);

            
            this.Refresh();
            this.Update();
            this.ResumeLayout(false);
        }


        int y = 10;
        int left_lbl_padding = 5;
        int left_txt_padding = 10;
        int width = 200;
        int height = 13;
        int padding = 5;
        int padding_large = 2;

        internal void Clear()
        {
            this.Controls.Clear();
            this.y = 10;
        }
    }
}
