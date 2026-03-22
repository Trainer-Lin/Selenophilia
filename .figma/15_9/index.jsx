import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.xRxrAppBar}>
      <div className={styles.textContent}>
        <p className={styles.headline} />
      </div>
      <div className={styles.leadingIcon}>
        <div className={styles.stateLayer}>
          <img src="../image/mn1cw17a-p45d8on.svg" className={styles.icon} />
        </div>
      </div>
      <div className={styles.trailingIcon}>
        <div className={styles.stateLayer}>
          <img src="../image/mn1cw17b-h4rswma.svg" className={styles.icon} />
        </div>
      </div>
    </div>
  );
}

export default Component;
