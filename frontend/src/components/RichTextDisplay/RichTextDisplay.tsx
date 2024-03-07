import * as React from 'react';
import './RichTextDisplay.css';

interface RichTextDisplayProps {
    content: string;
}

function RichTextDisplay(props: RichTextDisplayProps) {
    return (
        <div className='rich-text-display' dangerouslySetInnerHTML={{__html: props.content}}></div>
    );
}

export default RichTextDisplay;
