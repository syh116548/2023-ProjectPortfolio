import * as React from 'react';
import ReactQuill from 'react-quill';
import "./RichTextField.css";

interface RichTextFieldProps {
    value: string;
    onChange: (content: string) => void;
    placeholder: string;
    style: { marginTop: string; marginBottom: string; };
}

const modules = {
    toolbar: [
        ["bold", "italic", "underline"],
        [{ list: "ordered" }, { list: "bullet" }],
        ["link", "image"]
    ],
    clipboard: {
        matchVisual: false
    }
}

function RichTextField(props: RichTextFieldProps) {
    return (
        <ReactQuill
            modules={modules}
            value={props.value}
            theme="snow"
            onChange={props.onChange}
            placeholder={props.placeholder}
            className="editor"
            style={props.style}
        />
    );
};

export default RichTextField;